package starter.rest;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starter.RequestContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Etc {

    @Autowired
    private RequestContext context;

    @RequestMapping(value = "/svc/logout", method = RequestMethod.POST)
    public String logout() {
        SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    @RequestMapping(value = "analyze", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> analyze(@RequestParam String text, @RequestParam(defaultValue = "standard") String analyzer) {
        AnalyzeResponse response = context.getClient().admin().indices().prepareAnalyze(text).setAnalyzer(analyzer).execute().actionGet();
        List<String> words = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken analyzeToken : response.getTokens()) {
            words.add(analyzeToken.getTerm());
        }
        return words;
    }

    @RequestMapping(value = "i18n", method = RequestMethod.GET)
    public String analyze(@RequestParam String text) {
        return context.m(text);
    }


    @RequestMapping(value = "url", method = RequestMethod.GET)
    public String urlEncode(@RequestParam String text) throws IOException {
        return text;
    }

}
