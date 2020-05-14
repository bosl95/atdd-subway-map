package wooteco.subway.admin.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wooteco.subway.admin.domain.Line;
import wooteco.subway.admin.domain.LineStation;
import wooteco.subway.admin.domain.Station;
import wooteco.subway.admin.dto.request.LineStationCreateRequest;
import wooteco.subway.admin.dto.resopnse.LineDetailResponse;
import wooteco.subway.admin.dto.resopnse.StationResponse;
import wooteco.subway.admin.exception.NotFoundException;
import wooteco.subway.admin.repository.LineRepository;
import wooteco.subway.admin.repository.StationRepository;

@Service
public class LineStationService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public LineStationService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public List<LineDetailResponse> findLinesWithStations() {
        List<Line> lines = lineRepository.findAll();
        Map<Long, Station> stations = stationRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Station::getId, station -> station));
        return lines.stream()
            .map(line -> {
                List<StationResponse> stationResponses = line.getStationsId().stream()
                    .map(stations::get)
                    .map(StationResponse::of)
                    .collect(Collectors.toList());
                return LineDetailResponse.of(line, stationResponses);
            }).collect(Collectors.toList());
    }

    @Transactional
    public LineDetailResponse findLineWithStationsBy(Long lineId) {
        Line line = findLineBy(lineId);
        List<Long> stationsId = line.getStationsId();
        List<StationResponse> stations = getStationResponses(stationsId);
        return LineDetailResponse.of(line, stations);
    }

    @Transactional
    public void addStationInLine(Long lineId, LineStationCreateRequest lineStationCreateRequest) {
        Line line = findLineBy(lineId);
        LineStation lineStation = lineStationCreateRequest.toLineStation();
        validateStations(lineStation.getPreStationId(), lineStation.getStationId());
        line.addLineStation(lineStation);
        lineRepository.save(line);
    }

    @Transactional
    public void removeStationFromLine(Long lineId, Long stationId) {
        Line line = findLineBy(lineId);
        line.removeStationBy(stationId);
        lineRepository.save(line);
    }

    private void validateStations(Long preStationId, Long stationId) {
        if (Objects.isNull(stationId) || !stationRepository.existsById(stationId)) {
            throw new NotFoundException(stationId);
        }
        if (Objects.nonNull(preStationId) && !stationRepository.existsById(preStationId)) {
            throw new NotFoundException(preStationId);
        }
    }

    private List<StationResponse> getStationResponses(List<Long> stationsId) {
        Map<Long, Station> stations = stationRepository.findAllById(stationsId)
            .stream()
            .collect(Collectors.toMap(Station::getId, station -> station));
        return stationsId.stream()
            .map(stations::get)
            .map(StationResponse::of)
            .collect(Collectors.toList());
    }

    private Line findLineBy(Long id) {
        return lineRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(id));
    }
}
