package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.CommentEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 商品评价
 *
 * @author WH
 * @email 13665261843@qq.com
 * @date 2020-12-14 19:00:43
 */
public interface CommentService extends IService<CommentEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

