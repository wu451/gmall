package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import java.util.List;

/**
 * 属性分组
 *
 * @author WH
 * @email 13665261843@qq.com
 * @date 2020-12-14 19:00:43
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryGroupWithAttrsByCid(Long cid);
}

