package com.hat.hereandthere.chatservice.chat;

import com.hat.hereandthere.chatservice.chat.dto.ChatCountDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/places")
@RestController
public class PlaceController {
    final private PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping()
    public ResponseEntity<ChatCountDto> getChatCount(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "majorRegionId", required = false) Long majorRegionId,
            @RequestParam(value = "areaId", required = false) Long areaId,
            @RequestParam(value = "sigunguId", required = false) Long sigunguId
    ) {
        log.error("majorRegionId: {}, areaId: {}, sigunguId: {}", majorRegionId, areaId, sigunguId);
        if (majorRegionId != null && areaId == null && sigunguId == null) {
            return ResponseEntity.ok(placeService.getPlaceChatCountByMajorRegionId(page, pageSize, majorRegionId));
        }

        if (majorRegionId == null && areaId != null && sigunguId != null) {
            return ResponseEntity.ok(placeService.getPlaceChatCountBySigunguId(page, pageSize, areaId, sigunguId));
        }

        if (majorRegionId == null && areaId == null && sigunguId == null) {
            return ResponseEntity.ok(placeService.getPlaceChatCount(page, pageSize));
        }

        return ResponseEntity.badRequest().build();
    }
}
