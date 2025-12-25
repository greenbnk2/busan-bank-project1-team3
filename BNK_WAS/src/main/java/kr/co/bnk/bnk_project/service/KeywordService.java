package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.KeywordDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.mapper.KeywordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordMapper keywordMapper;

    public PageResponseDTO<KeywordDTO> getKeywordPage(PageRequestDTO pageRequestDTO) {
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        int total = keywordMapper.selectKeywordTotal(pageRequestDTO);
        List<KeywordDTO> list = keywordMapper.selectAllKeyword(pageRequestDTO);

        return PageResponseDTO.of(pageRequestDTO, list, total);
    }

    public void createKeyword(KeywordDTO dto) {
        keywordMapper.insertKeyword(dto);
    }

    public void updateKeyword(KeywordDTO dto) {
        keywordMapper.updateKeyword(dto);
    }

    public void deleteKeyword(String keywordNo) {
        keywordMapper.deleteKeyword(keywordNo);
    }

}
