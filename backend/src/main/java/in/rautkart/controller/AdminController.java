package in.rautkart.controller;

import in.rautkart.dto.AdminDtos;
import in.rautkart.dto.CatalogDtos;
import in.rautkart.dto.OrderDtos;
import in.rautkart.service.AdminService;
import in.rautkart.service.CatalogService;
import in.rautkart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Everything behind the admin panel. The whole path is locked to ROLE_ADMIN. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CatalogService catalogService;
    private final OrderService orderService;
    private final AdminService adminService;

    public AdminController(CatalogService catalogService, OrderService orderService, AdminService adminService) {
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.adminService = adminService;
    }

    // ----------------------------------------------------------------- products

    @GetMapping("/products")
    public CatalogDtos.PageResponse<CatalogDtos.ProductResponse> products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return catalogService.listAllForAdmin(page, size);
    }

    @PostMapping("/products")
    public ResponseEntity<CatalogDtos.ProductResponse> createProduct(
            @Valid @RequestBody CatalogDtos.ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public CatalogDtos.ProductResponse updateProduct(@PathVariable Long id,
                                                     @Valid @RequestBody CatalogDtos.ProductRequest request) {
        return catalogService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        catalogService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------------- categories

    @PostMapping("/categories")
    public ResponseEntity<CatalogDtos.CategoryResponse> createCategory(
            @Valid @RequestBody CatalogDtos.CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createCategory(request));
    }

    // ------------------------------------------------------------------- orders

    @GetMapping("/orders")
    public CatalogDtos.PageResponse<OrderDtos.OrderResponse> orders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderService.allOrders(status, page, size);
    }

    @PatchMapping("/orders/{id}/status")
    public OrderDtos.OrderResponse updateStatus(@PathVariable Long id,
                                                @Valid @RequestBody OrderDtos.UpdateStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    // ---------------------------------------------------------------- dashboard

    @GetMapping("/dashboard")
    public AdminDtos.DashboardResponse dashboard() {
        return adminService.dashboard();
    }
}
