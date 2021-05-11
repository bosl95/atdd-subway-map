package wooteco.subway.line.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wooteco.subway.line.domain.Line;
import wooteco.subway.line.dto.LineRequest;
import wooteco.subway.line.dto.LineResponse;
import wooteco.subway.line.service.LineService;
import wooteco.subway.line.service.SectionService;
import wooteco.subway.station.dto.StationResponse;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/lines")
@RestController
public class LineController {

    private final LineService lineService;
    private final SectionService sectionService;

    public LineController(LineService lineService, SectionService sectionService) {
        this.lineService = lineService;
        this.sectionService = sectionService;
    }

    @PostMapping
    public ResponseEntity<LineResponse> createLine(@RequestBody @Valid LineRequest lineRequest) {
        Long id = lineService.save(lineRequest.toLinesEntity());
        sectionService.save(id, lineRequest.getUpStationId(), lineRequest.getDownStationId(), lineRequest.getDistance());
        Line newLine = lineService.findById(id);
        return ResponseEntity.created(
                URI.create("/lines/" + newLine.getId()))
                .body(new LineResponse(newLine));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LineResponse>> showLines() {
        List<Line> lines = lineService.findAll();
        List<LineResponse> lineResponses = lines.stream()
                .map(LineResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lineResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LineResponse> showSections(@PathVariable Long id) {
        Line line = lineService.findById(id);
        List<StationResponse> section = sectionService.findSectionById(id);
        return ResponseEntity.ok(new LineResponse(line, section));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateLine(@PathVariable Long id, @RequestBody @Valid LineRequest lineRequest) {
        Line line = lineRequest.toLineEntity();
        lineService.update(id, line);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLine(@PathVariable Long id) {
        lineService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sections")
    public ResponseEntity<LineResponse> addSection(@PathVariable Long id, @RequestBody @Valid LineRequest lineRequest) {
        sectionService.saveSectionOfExistLine(id, lineRequest.getUpStationId(), lineRequest.getDownStationId(), lineRequest.getDistance());
        Line line = lineService.findById(id);
        List<StationResponse> section = sectionService.findSectionById(id);
        return ResponseEntity.ok(new LineResponse(line, section));
    }

    @DeleteMapping("/{id}/sections")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id, @RequestParam Long stationId) {
        Line line = lineService.findById(id);
        sectionService.deleteSection(line, stationId);
        return ResponseEntity.ok().build();
    }
}
