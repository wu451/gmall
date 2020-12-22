package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * 商品属性
 * 
 * @author WH
 * @email 13665261843@qq.com
 * @date 2020-12-14 19:00:43
 */
@Mapper
@Component
public interface AttrMapper extends BaseMapper<AttrEntity> {
	
}
