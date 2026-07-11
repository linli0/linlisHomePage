package com.coffeecookies.homepage.config;

import com.coffeecookies.homepage.entity.*;
import com.coffeecookies.homepage.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ArticleRepository articleRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing sample data...");
        
        initUsers();
        initExchangeRates();
        initCategories();
        initTags();
        initArticles();
        
        log.info("Sample data initialization completed.");
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@coffeecookies.com");
            admin.setDisplayName("Administrator");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@coffeecookies.com");
            user.setDisplayName("User");
            user.setRole(User.Role.USER);
            userRepository.save(user);
            
            log.info("Created default users: admin/admin123, user/user123");
        }
    }

    private void initExchangeRates() {
        if (exchangeRateRepository.count() == 0) {
            exchangeRateRepository.save(createRate("USD", "USD", "1.0"));
            exchangeRateRepository.save(createRate("USD", "CNY", "7.20"));
            exchangeRateRepository.save(createRate("USD", "EUR", "0.92"));
            exchangeRateRepository.save(createRate("USD", "GBP", "0.79"));
            log.info("Initialized exchange rates");
        }
    }

    private ExchangeRate createRate(String from, String to, String rate) {
        ExchangeRate er = new ExchangeRate();
        er.setFromCurrency(from);
        er.setToCurrency(to);
        er.setRate(new BigDecimal(rate));
        return er;
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(createCategory("技术", "Technology articles", "💻", 1));
            categoryRepository.save(createCategory("生活", "Life articles", "🌟", 2));
            categoryRepository.save(createCategory("投资", "Investment articles", "📈", 3));
            categoryRepository.save(createCategory("杂谈", "Miscellaneous", "📝", 4));
            log.info("Initialized categories");
        }
    }

    private Category createCategory(String name, String desc, String icon, int order) {
        Category cat = new Category();
        cat.setName(name);
        cat.setDescription(desc);
        cat.setIcon(icon);
        cat.setSortOrder(order);
        return cat;
    }

    private void initTags() {
        if (tagRepository.count() == 0) {
            tagRepository.save(createTag("Java", "#f89820"));
            tagRepository.save(createTag("Spring", "#6db33f"));
            tagRepository.save(createTag("Vue", "#42b883"));
            tagRepository.save(createTag("JavaScript", "#f7df1e"));
            tagRepository.save(createTag("Gold", "#ffd700"));
            tagRepository.save(createTag("Investment", "#00c853"));
            log.info("Initialized tags");
        }
    }

    private Tag createTag(String name, String color) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setColor(color);
        return tag;
    }

    private void initArticles() {
        if (articleRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElseThrow();
            Category tech = categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equals("技术")).findFirst().orElseThrow();
            Tag java = tagRepository.findByName("Java").orElseThrow();
            Tag spring = tagRepository.findByName("Spring").orElseThrow();
            
            Article article1 = new Article();
            article1.setTitle("欢迎使用 CoffeeCookies Homepage");
            article1.setContent("# 欢迎使用 CoffeeCookies Homepage\n\n" +
                    "这是一个集成了多种功能的全栈应用。\n\n" +
                    "## 功能特性\n\n" +
                    "- 🔐 用户认证系统（JWT）\n" +
                    "- 💰 实时金价查询\n" +
                    "- 📊 价格走势图表\n" +
                    "- 🛠️ 实用工具箱\n" +
                    "- 📝 文章管理\n\n" +
                    "## 技术栈\n\n" +
                    "- 后端：Spring Boot 3.2 + Java 17\n" +
                    "- 前端：Vue 3 + Vite\n" +
                    "- 数据库：H2 (开发) / MySQL (生产)\n");
            article1.setSummary("介绍 CoffeeCookies Homepage 的功能和技术栈");
            article1.setPublished(true);
            article1.setAuthor(admin);
            article1.setCategory(tech);
            article1.setTags(Set.of(java, spring));
            articleRepository.save(article1);
            
            Article article2 = new Article();
            article2.setTitle("黄金价格走势分析");
            article2.setContent("# 黄金价格走势分析\n\n" +
                    "黄金作为一种重要的投资品种，其价格受多种因素影响。\n\n" +
                    "## 影响因素\n\n" +
                    "1. **美元汇率** - 黄金与美元通常呈负相关\n" +
                    "2. **通胀预期** - 通胀上升推动黄金需求\n" +
                    "3. **地缘政治** - 不确定性增加黄金避险需求\n" +
                    "4. **央行政策** - 利率变化影响持有黄金的机会成本\n\n" +
                    "## 投资建议\n\n" +
                    "投资有风险，入市需谨慎。建议分散投资，不要将所有资金投入单一品种。");
            article2.setSummary("分析影响黄金价格的主要因素");
            article2.setPublished(true);
            article2.setAuthor(admin);
            article2.setCategory(categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equals("投资")).findFirst().orElseThrow());
            article2.setTags(Set.of(tagRepository.findByName("Gold").orElseThrow(),
                    tagRepository.findByName("Investment").orElseThrow()));
            articleRepository.save(article2);
            
            log.info("Initialized sample articles");
        }
    }
}
