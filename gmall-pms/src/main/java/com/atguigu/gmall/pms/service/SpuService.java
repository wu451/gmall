package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.SpuEntity;

import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * spu信息
 *
 * @author WH
 * @email 13665261843@qq.com
 * @date 2020-12-14 19:00:43
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo queryPageInfo(PageParamVo pageParamVo, Long categoryId);

    void bigSave(SpuVo spu);
}

