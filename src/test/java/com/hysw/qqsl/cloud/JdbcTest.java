package com.hysw.qqsl.cloud;

import com.hysw.qqsl.cloud.core.entity.data.Article;
import com.hysw.qqsl.cloud.core.service.ArticleService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by Administrator on 2016/10/28.
 */
public class JdbcTest extends BaseTest {

    @Autowired
    private ArticleService articleService;
    private JdbcTemplate template;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        template = new JdbcTemplate(dataSource);
    }

    @Before
    public void cleanData() {
        String script = "qqslArticle.sql";
        ClassPathResource resource = new ClassPathResource(script);
        JdbcTestUtils.executeSqlScript(template, resource, true);
    }
    @Test
    public void test(){
        List<Article> articles = articleService.findAll();
        Assert.assertNotNull(articles);
    }
}
