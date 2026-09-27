package org.vced.ragutillib.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    public CustomImage getImageById(Long id) {
        return imageRepository.findById(id).orElseThrow(RuntimeException::new);
    }
}
