package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.*;

import com.atguigu.gmall.pms.feign.GmallPmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Autowired
   private SpuDescMapper spuDescMapper;
    @Autowired
    private SpuAttrValueService spuAttrValueService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SkuImagesService imagesService;
    @Autowired
    private SkuAttrValueService attrValueService;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private SpuDescService descService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo queryPageInfo(PageParamVo pageParamVo, Long categoryId) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果用户选择了本类,并查询本类
        if(categoryId!=0){
            wrapper.eq("category_id", categoryId);
        }
        String key  = pageParamVo.getKey();
        //判断关键字是否为空
        if(StringUtils.isNotBlank(key)){
            wrapper.and((t) -> {
               t.eq("id", key).or().like("name", key);
            });
        }

        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );
        //把Ipage装换成PageResult对象
        return new PageResultVo(page);
    }
    //保存spo sko大表单
    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spu) {
        //1保存spu信息
        //1.1. spu基本信息 pms_spu
        saveSpu(spu);
        //1.2. spu描述信息pms_spu_desc
        //saveSpuDesc(spu);
        this.descService.saveSpuDesc(spu);

        //1.3. spu基本属性信息 pms_spu_attr_value   map 集合转化方法
        saveBaseAttr(spu);

        //2保存sku信息
        saveSku(spu);
        //消息队列
        this.rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE", "item.insert", spu.getId());
    }

    private void saveSku(SpuVo spu) {
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        //2.1. sku基本信息 pms_sku
        skus.forEach(sku ->{
            sku.setSpuId(spu.getId());
            sku.setBrandId(spu.getBrandId());
            sku.setCatagoryId(spu.getCategoryId());
            //默认图片
           List<String> images =  sku.getImages();
            if(!CollectionUtils.isEmpty(images)){
                sku.setDefaultImage(StringUtils.isNotBlank(sku.getDefaultImage())?sku.getDefaultImage():images.get(0));
            }
            this.skuMapper.insert(sku);
            //2.2. sku图片 pms_sku_images
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(sku.getId());
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(),image)?1:0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                imagesService.saveBatch(skuImagesEntities);
            }
            //2.3. sku的销售属性 pms_sku_value
            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
               saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(sku.getId()));
                this.attrValueService.saveBatch(saleAttrs);
            }

            //3保存sku营销信息
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(sku, skuSaleVo);
            skuSaleVo.setSkuId(sku.getId());
            this.gmallPmsClient.saveSales(skuSaleVo);
            //3.1. sku积分优惠  sms_sku_bounds

            //3.2 sku满减信息  sms_sku_full_reduction
            //3.3 sku打折信息 sms_sku_ladder
        });
    }

    private void saveBaseAttr(SpuVo spu) {
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){

            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spu.getId());
                return spuAttrValueEntity;
            }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }


    private void saveSpu(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
    }

}