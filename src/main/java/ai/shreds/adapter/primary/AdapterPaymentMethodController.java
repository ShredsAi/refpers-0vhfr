package ai.shreds.adapter.primary;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ai.shreds.application.dtos.ApplicationPaymentMethodActivationDTO;
import ai.shreds.application.dtos.ApplicationPaymentMethodRequestDTO;
import ai.shreds.application.dtos.ApplicationPaymentMethodResponseDTO;
import ai.shreds.application.ports.ApplicationInputPortPaymentMethod;
import ai.shreds.shared.dtos.SharedPaymentMethodActivationResponseDTO;
import ai.shreds.shared.dtos.SharedPaymentMethodRequestDTO;
import ai.shreds.shared.dtos.SharedPaymentMethodResponseDTO;

@RestController
@RequestMapping("/payment-methods")
public class AdapterPaymentMethodController {

    private final ApplicationInputPortPaymentMethod paymentMethodService;

    public AdapterPaymentMethodController(ApplicationInputPortPaymentMethod paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @PostMapping
    public ResponseEntity<SharedPaymentMethodResponseDTO> addPaymentMethod(
            @Valid @RequestBody SharedPaymentMethodRequestDTO request) {
        ApplicationPaymentMethodRequestDTO appRequest = request.toApplicationDTO();
        ApplicationPaymentMethodResponseDTO appResponse = paymentMethodService.addPaymentMethod(appRequest);
        SharedPaymentMethodResponseDTO response = SharedPaymentMethodResponseDTO.fromApplicationDTO(appResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<SharedPaymentMethodActivationResponseDTO> activatePaymentMethod(
            @PathVariable("id") UUID paymentMethodId) {
        ApplicationPaymentMethodActivationDTO appResponse = paymentMethodService.activatePaymentMethod(paymentMethodId.toString());
        SharedPaymentMethodActivationResponseDTO response = SharedPaymentMethodActivationResponseDTO.fromApplicationDTO(appResponse);
        return ResponseEntity.ok(response);
    }
}