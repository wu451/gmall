package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author WH
 * @email 13665261843@qq.com
 * @date 2021-01-11 17:16:53
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
