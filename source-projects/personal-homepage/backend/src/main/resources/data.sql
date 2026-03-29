-- Initialize sample data

-- Categories
INSERT INTO categories (name, description, sort_order, created_at, updated_at) VALUES 
    ('Technology', 'Technical articles about programming and software development', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Life', 'Personal life and thoughts', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Tutorial', 'Step-by-step tutorials and guides', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Tags
INSERT INTO tags (name, color, created_at) VALUES 
    ('Java', '#007396', CURRENT_TIMESTAMP),
    ('Spring Boot', '#6DB33F', CURRENT_TIMESTAMP),
    ('Vue.js', '#4FC08D', CURRENT_TIMESTAMP),
    ('JavaScript', '#F7DF1E', CURRENT_TIMESTAMP),
    ('Docker', '#2496ED', CURRENT_TIMESTAMP),
    ('Database', '#4479A1', CURRENT_TIMESTAMP);

-- Skills
INSERT INTO skills (name, category, proficiency_level, icon, sort_order, created_at, updated_at) VALUES 
    ('Java', 'Backend', 90, 'fab fa-java', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Spring Boot', 'Backend', 85, 'fas fa-leaf', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MySQL', 'Database', 80, 'fas fa-database', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Redis', 'Database', 75, 'fas fa-bolt', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Vue.js', 'Frontend', 85, 'fab fa-vuejs', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TypeScript', 'Frontend', 80, 'fab fa-js', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Docker', 'DevOps', 70, 'fab fa-docker', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Git', 'Tools', 85, 'fab fa-git-alt', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Projects
INSERT INTO projects (name, description, url, github_url, image, category, technologies, start_date, end_date, sort_order, is_featured, created_at, updated_at) VALUES 
    ('Personal Homepage', 'A full-stack personal homepage with blog, file management, and tools', 
     'https://example.com', 'https://github.com/example/homepage', 
     '/images/project1.jpg', 'Web Development', 
     'Java,Spring Boot,Vue.js,TypeScript', 
     '2024-01-01', NULL, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('E-commerce Platform', 'A microservices-based e-commerce platform', 
     NULL, 'https://github.com/example/ecommerce', 
     '/images/project2.jpg', 'Web Development', 
     'Java,Spring Cloud,Docker,MySQL', 
     '2023-06-01', '2023-12-01', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Article
INSERT INTO articles (title, summary, content, cover_image, view_count, is_published, is_top, category_id, created_at, updated_at) VALUES 
    ('Welcome to My Personal Homepage', 
     'This is the first article on my personal homepage. Here I will share my thoughts and experiences.', 
     '# Welcome\n\nThis is a sample article to get you started.\n\n## Features\n\n- Blog system\n- File management\n- Useful tools\n\nEnjoy!', 
     '/images/default-cover.jpg', 
     100, true, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Link article to tag
INSERT INTO article_tags (article_id, tag_id) VALUES (1, 1), (1, 2), (1, 3);
