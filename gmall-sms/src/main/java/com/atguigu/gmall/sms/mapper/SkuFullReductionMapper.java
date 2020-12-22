package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 商品满减信息
 * 
 * @author WH
 * @email 13665261843@qq.com
 * @date 2020-12-14 20:19:13
 */

@Mapper
public interface SkuFullReductionMapper extends BaseMapper<SkuFullReductionEntity> {
	
}
