package com.rota.order.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.NotFoundException;
import com.rota.order.domain.coupon.Coupon;
import com.rota.order.domain.coupon.CouponRepository;
import com.rota.order.interfaces.rest.dto.CouponDtos.CouponRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository coupons;
    private final Clock clock;

    public CouponService(CouponRepository coupons, Clock clock) {
        this.coupons = coupons;
        this.clock = clock;
    }

    /**
     * Valida o cupom e calcula o desconto, sem consumir um uso.
     */
    @Transactional(readOnly = true)
    public BigDecimal evaluate(String code, BigDecimal subtotal, Long restaurantId) {
        return find(code).discountFor(subtotal, restaurantId, clock.instant());
    }

    /**
     * Valida e consome um uso do cupom. Deve rodar na mesma transação que grava o pedido.
     */
    @Transactional
    public BigDecimal redeem(String code, BigDecimal subtotal, Long restaurantId) {
        Coupon coupon = find(code);
        BigDecimal discount = coupon.discountFor(subtotal, restaurantId, clock.instant());
        if (coupons.incrementUsage(coupon.getId()) == 0) {
            throw new BusinessException("Cupom " + coupon.getCode() + " esgotado");
        }
        return discount;
    }

    @Transactional(readOnly = true)
    public List<Coupon> list() {
        return coupons.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Coupon create(CouponRequest request) {
        String code = Coupon.normalize(request.code());
        if (coupons.existsByCode(code)) {
            throw new ConflictException("Já existe um cupom com o código " + code);
        }
        return coupons.save(new Coupon(code, request.description(), request.type(), request.value(),
                request.minOrderValue(), request.maxDiscount(), request.validUntil(), request.usageLimit(),
                request.restaurantId()));
    }

    @Transactional
    public Coupon setActive(Long id, boolean active) {
        Coupon coupon = coupons.findById(id).orElseThrow(() -> NotFoundException.of("Cupom", id));
        coupon.setActive(active);
        return coupon;
    }

    private Coupon find(String code) {
        return coupons.findByCode(Coupon.normalize(code))
                .orElseThrow(() -> new NotFoundException("Cupom não encontrado: " + Coupon.normalize(code)));
    }
}
