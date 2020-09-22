/**
 * 
 */
package starter.cfsrest.rest;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import starter.cfsrest.bean.Constant;
import starter.cfsrest.service.CFSTreeService;

/**
 * @author JackIce
 * @date 2016年5月26日
 * @description
 */
@RestController
@RequestMapping(value = "/svc", produces = MediaType.APPLICATION_JSON_VALUE)
public class CFSTreeRest {
	@Autowired
	private CFSTreeService cfsTreeService;

	/*
	 * 查询文档类型的条码信息，用于文档类型条码打印中的显示
	 */
	@RequestMapping(value = "/tree_data/{type}", method = { RequestMethod.GET })
	public String queryTreeData(@PathVariable String type,
			@RequestParam(defaultValue = "0") int start,
			@RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "") String funCode) {
		XContentBuilder builder = null;
		String treeData = null;
		try {
			builder = cfsTreeService.query(type, start, limit, funCode,
					Constant.CFS_COMMON_DIGIT_STRING_ZERO);
			treeData = builder.string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return treeData;
	}

	

}
