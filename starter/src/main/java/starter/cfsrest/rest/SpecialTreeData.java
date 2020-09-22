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
 * @date  2016年6月12日
 * @description 
 */

@RestController
@RequestMapping(value = "/tree", produces = MediaType.APPLICATION_JSON_VALUE)
public class SpecialTreeData {
	@Autowired
	private CFSTreeService cfsTreeService;
	/*
	 * 根据类型type，功能码funcode进行查询树形结构数据
	 */
	@RequestMapping(value = "/tree_data/{type}", method = {RequestMethod.GET})
	public String queryTreeData(@PathVariable String type,
			@RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String funCode
            ){
		XContentBuilder builder = null;
		String treeData = null;
		try {
			builder = cfsTreeService.query(type, start, limit);
			treeData = builder.string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return treeData;
	}
}
