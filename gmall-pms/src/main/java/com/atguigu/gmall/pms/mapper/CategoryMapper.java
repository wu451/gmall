package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author WH
 * @email 13665261843@qq.com
 * @date 2020-12-14 19:00:43
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
    List<CategoryEntity> queryCategoriesWithSubsByPid(Long parentId);
	
}
