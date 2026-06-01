//package com.freshmart.security;
//
//import com.freshmart.utils.JwtUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.HttpStatusEntryPoint;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private UserDetailsServiceImpl userDetailsService;
//
//    @Autowired
//    private JwtUtils jwtUtils;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }
//
//    @Bean
//    public JwtAuthenticationFilter jwtAuthenticationFilter() {
//        return new JwtAuthenticationFilter();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. Kích hoạt xử lý CORS liên cổng giữa Frontend và Backend
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//                // 2. Tắt bảo vệ CSRF vì hệ thống chạy Stateless REST API dựa trên JWT Token
//                .csrf(csrf -> csrf.disable())
//
//                // 3. Quản lý trạng thái Session
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // ⚠️ ĐÃ XÓA DÒNG anonymous.disable() để permitAll() thực sự có hiệu lực cho request không token
//
//                // 4. Xử lý phản hồi lỗi Xác thực mặc định trả về 401 Unauthorized thay vì trang Login HTML
//                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
//
//                // 5. Định tuyến phân quyền chi tiết các Endpoint API
//                .authorizeHttpRequests(authz -> authz
//                        // Cho phép đăng ký, đăng nhập tự do không cần token
//                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
//
//                        // --- PHÂN QUYỀN API SẢN PHẨM (PRODUCTS) ---
//                        // Bất kỳ ai cũng có thể xem danh sách và chi tiết sản phẩm
//                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
//                        // Chỉ ADMIN và STAFF mới có quyền tạo, sửa, xóa sản phẩm
//                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
//                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
//                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
//
//                        // --- PHÂN QUYỀN API ĐƠN HÀNG (ORDERS) ---
//                        // ADMIN và STAFF có quyền xem toàn bộ đơn hàng, cập nhật trạng thái hoặc xóa
//                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("ADMIN", "STAFF")
//                        .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAnyRole("ADMIN", "STAFF")
//                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasAnyRole("ADMIN", "STAFF")
//                        // Khách hàng (CUSTOMER) muốn đặt hàng (POST) phải đăng nhập (sẽ chặn bằng anyRequest hoặc cấu hình riêng nếu có endpoint khách đặt)
//
//                        // --- DANH MỤC VÀ ĐÁNH GIÁ ---
//                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
//                        // Thêm/Sửa/Xóa danh mục chỉ dành cho ban quản trị
//                        .requestMatchers("/api/categories/**").hasAnyRole("ADMIN", "STAFF")
//
//                        // Mọi hành động thao tác khác bắt buộc phải đăng nhập/xác thực Token
//                        .anyRequest().authenticated()
//                );
//
//        // 6. Tích hợp Filter kiểm tra JWT Token trước khi xử lý nghiệp vụ
//        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    // 7. Cấu hình chi tiết CORS Source chặn tình trạng lỗi Cross-Origin trên trình duyệt
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Cho phép nhận yêu cầu từ bất kỳ nguồn (Origin) nào phát tới Backend
//        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Chỉ đích danh địa chỉ frontend
//        configuration.setAllowCredentials(true);
//
//        // Hỗ trợ đầy đủ các phương thức HTTP cơ bản cho các tác vụ CRUD sản phẩm
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//
//        // Chấp nhận tất cả các định dạng Header truyền tải dữ liệu lên hệ thống
//        configuration.setAllowedHeaders(List.of("*"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//    }
//}
package com.freshmart.config;

import com.freshmart.security.CustomUserDetailsService;
import com.freshmart.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity // Cho phép dùng @PreAuthorize("hasRole('ADMIN')") ở Controller
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public JwtAuthenticationFilter authenticationJwtTokenFilter() {
        return new JwtAuthenticationFilter();
    }

    // Mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Quản lý xác thực
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Cấu hình CORS để Frontend (HTML/JS) gọi được API mà không bị chặn
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Cho phép mọi domain gọi API
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // LUẬT BẢO MẬT CHÍNH
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Mở cửa tự do cho Đăng nhập, Đăng ký
                        .requestMatchers("/api/auth/**").permitAll()
                        // Mở cửa tự do cho khách xem Sản phẩm, Danh mục, Đánh giá
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        // Bắt buộc quyền Admin cho việc thêm, sửa, xóa sản phẩm, danh mục...
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Mọi request khác đều phải đăng nhập
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        // Chèn bộ lọc JWT vào trước bộ lọc UsernamePassword mặc định của Spring
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}