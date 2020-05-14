package wooteco.subway.admin.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wooteco.subway.admin.domain.Line;
import wooteco.subway.admin.domain.Station;
import wooteco.subway.admin.dto.LineRequest;
import wooteco.subway.admin.dto.LineResponse;
import wooteco.subway.admin.dto.LineStationCreateRequest;
import wooteco.subway.admin.dto.StationResponse;
import wooteco.subway.admin.service.LineService;

@RestController
@RequestMapping("/lines")
public class LineController {

    private LineService lineService;

    public LineController(LineService lineService) {
        this.lineService = lineService;
    }

    @PostMapping
    public ResponseEntity<LineResponse> createLines(@RequestBody LineRequest request) {
        Line line = lineService.save(request.toLine());

        return ResponseEntity
            .created(URI.create("/lines/" + line.getId()))
            .body(LineResponse.of(line));
    }

    @GetMapping
    public ResponseEntity<List<LineResponse>> getLines() {
        return ResponseEntity.ok()
            .body(lineService.findAllLineWithStations());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LineResponse> updateLine(
        @PathVariable Long id,
        @RequestBody LineRequest request) {
        Line line = lineService.updateLine(id, request.toLine());

        return ResponseEntity.ok()
            .body(LineResponse.of(line));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LineResponse> getLine(@PathVariable Long id) {
        return ResponseEntity.ok()
            .body(lineService.findLineWithStationsById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLine(@PathVariable Long id) {
        lineService.deleteLineById(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stations")
    public ResponseEntity<List<StationResponse>> addStationToLine(
        @PathVariable Long id,
        @RequestBody LineStationCreateRequest request) {
        List<Station> response = lineService.addLineStation(id, request.toLineStation());

        return ResponseEntity
            .created(URI.create("/lines/" + id + "/stations/" + request.getStationId()))
            .body(StationResponse.listOf(response));
    }

    @GetMapping("/{id}/stations")
    public ResponseEntity<List<StationResponse>> getStationsOfLine(@PathVariable Long id) {
        List<Station> response = lineService.findStationsOf(id);

        return ResponseEntity
            .ok()
            .body(StationResponse.listOf(response));
    }

    @DeleteMapping("/{lineId}/stations/{stationId}")
    public ResponseEntity<Void> removeLineStation(
        @PathVariable Long lineId,
        @PathVariable Long stationId) {
        lineService.removeLineStation(lineId, stationId);

        return ResponseEntity.noContent().build();
    }

}