package com.hysw.qqsl.cloud;

import com.hysw.qqsl.cloud.core.entity.data.Article;
import com.hysw.qqsl.cloud.core.service.ArticleService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 2016/10/28.
 */
@Ignore
public class JdbcTest extends BaseTest {

    @Autowired
    private ArticleService articleService;
    private JdbcTemplate template;
    @Autowired
    private DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        template = new JdbcTemplate(dataSource);
    }

    @Before
    public void cleanData() throws SQLException {
        String script = "qqslArticle.sql";
        ClassPathResource resource = new ClassPathResource(script);
        Connection connection = dataSource.getConnection();
        ScriptUtils.executeSqlScript(connection, resource);
    }
    @Test
    public void test(){
        List<Article> articles = articleService.findAll();
        Assert.assertNotNull(articles);
    }
}
