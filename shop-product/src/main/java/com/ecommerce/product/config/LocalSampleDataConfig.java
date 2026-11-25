package com.ecommerce.product.config;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class LocalSampleDataConfig {

    @Bean
    CommandLineRunner seedLocalProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }
            productRepository.saveAll(List.of(
                    Product.builder()
                            .name("프로 사운드 무선 이어폰")
                            .price(129000L)
                            .stockQuantity(25)
                            .sellerId(1L)
                            .description("액티브 노이즈 캔슬링과 최대 24시간 재생을 지원하는 초경량 블루투스 이어폰입니다. IPX4 생활 방수로 출퇴근과 운동 중에도 편안하게 사용할 수 있습니다.")
                            .tags("무선이어폰,노이즈캔슬링,블루투스,경량")
                            .build(),
                    Product.builder()
                            .name("저소음 텐키리스 기계식 키보드")
                            .price(89000L)
                            .stockQuantity(12)
                            .sellerId(1L)
                            .description("사무실과 야간 작업에 적합한 저소음 적축 스위치를 적용한 컴팩트 텐키리스 기계식 키보드입니다. 유무선 겸용과 이중사출 PBT 키캡을 제공합니다.")
                            .tags("기계식키보드,저소음적축,텐키리스,사무용")
                            .build(),
                    Product.builder()
                            .name("무선 충전 스마트 데스크 램프")
                            .price(54000L)
                            .stockQuantity(4)
                            .sellerId(2L)
                            .description("눈부심 방지 확산판과 3단계 색온도 조절 기능을 갖춘 스탠드 조명입니다. 받침대 일체형 15W 무선 충전 패드로 스마트폰 충전이 간편합니다.")
                            .tags("데스크램프,스탠드,무선충전,홈오피스")
                            .build()));
        };
    }
}
