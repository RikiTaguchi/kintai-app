package com.example.api;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * PostgreSQL Testcontainers を使った統合テストの基底クラス。
 * コンテナは JVM 全体で1個の shared container として起動され、
 * 継承したすべてのテストクラスで共有される（Ryuk が JVM 終了時に破棄する）。
 *
 * <p>各テストクラスは Spring コンテキストを独立して立てるため、
 * {@code @ServiceConnection}（コンテキスト跨ぎでコンテナ参照が切断される問題あり）
 * ではなく {@link DynamicPropertySource} で JDBC URL を明示的に差し替える。
 *
 * <p>利用側は本クラスを継承し、{@code @SpringBootTest} または {@code @MybatisTest}
 * 等のスライステストを付与する。
 */
public abstract class AbstractPostgresIT {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    ).withDatabaseName("kintai_app_test")
     .withUsername("postgres")
     .withPassword("password");

    static {
        POSTGRES.start();
    }

    /**
     * 単体 terraform 用にはこちらを public static にしておくと、
     * 別のテストから直接参照（JDBC URL の取得等）もできる。
     */
    public static PostgreSQLContainer<?> postgres() {
        return POSTGRES;
    }

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }
}
