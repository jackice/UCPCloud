package starter.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import starter.uContentException;
import starter.service.DownloadSerive;

@RestController
@RequestMapping(value = "/svc", produces = MediaType.APPLICATION_JSON_VALUE)
public class Download {
	@Autowired
	private DownloadSerive downloadService;
	 @RequestMapping(value = "/{Type}/_download", method = {RequestMethod.GET})
	public void downloadZip(
			@PathVariable String Type,
			@RequestParam(defaultValue = "") String Query,
			@RequestParam(defaultValue = "") String DisctoryStructure,
			@RequestParam(defaultValue = "false") boolean Fulltext,
			@RequestParam(defaultValue = "0") int Start,
			@RequestParam(defaultValue = "10") int Limit,
			@RequestParam(defaultValue = "[]") SortBuilder[] sort,
			@RequestParam(defaultValue = "false") boolean allowableActions,
			@RequestParam(defaultValue = "true") boolean containsType,
			HttpServletRequest request,
			HttpServletResponse response
			
		){
		 try {
			 Query = java.net.URLDecoder.decode(Query, "UTF-8");
			this.downloadService.downLoad(Type, Query, DisctoryStructure, Fulltext, Start, Limit, sort, allowableActions, containsType, request, response);
		} catch (IOException e) {
			throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
