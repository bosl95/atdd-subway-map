package wooteco.subway.admin.domain.vo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.relational.core.mapping.MappedCollection;
import wooteco.subway.admin.domain.LineStation;

public class Stations {

    private static final int FIRST = 0;

    @MappedCollection(keyColumn = "line_key")
    private List<LineStation> stations;

    public Stations (List<LineStation> stations) {
        this.stations = stations;
    }

    public void addLineStation(LineStation lineStation) {
        if (lineStation.isFirst()) {
            addFirst(lineStation);
            return;
        }

        if (hasNoSuchPreStation(lineStation)) {
            throw new NoSuchElementException("이전 역이 등록되지 않았습니다.");
        }

        Optional<LineStation> nextStation = findNextStationBy(lineStation.getPreStationId());
        if (nextStation.isPresent()) {
            addBetweenTwo(lineStation, nextStation.get());
            return;
        }

        stations.add(lineStation);
    }

    private void addFirst(LineStation lineStation) {
        stations.stream()
            .findFirst()
            .ifPresent(station -> station.updatePreLineStation(lineStation.getStationId()));
        stations.add(FIRST, lineStation);
    }

    private void addBetweenTwo(LineStation lineStation, LineStation nextStation) {
        nextStation.updatePreLineStation(lineStation.getStationId());
        int position = stations.indexOf(nextStation);
        stations.add(position, lineStation);
    }

    private boolean hasNoSuchPreStation(LineStation lineStation) {
        return stations.stream()
            .map(LineStation::getStationId)
            .noneMatch(id -> lineStation.getPreStationId().equals(id));
    }

    private Optional<LineStation> findNextStationBy(Long stationId) {
        return stations.stream()
            .filter(station -> stationId.equals(station.getPreStationId()))
            .findFirst();
    }

    public void removeLineStationById(Long stationId) {
        LineStation station = findStationBy(stationId);
        findNextStationBy(stationId)
            .ifPresent(nextStation -> nextStation.updatePreLineStation(station.getPreStationId()));
        stations.remove(station);
    }

    private LineStation findStationBy(Long stationId) {
        return stations.stream()
            .filter(lineStation -> lineStation.getStationId().equals(stationId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("해당 노선에 등록되지 않은 역입니다."));
    }

    public List<Long> getStationIds() {
        return stations.stream()
            .map(LineStation::getStationId)
            .collect(Collectors.toList());
    }

    public List<LineStation> getStations() {
        return stations;
    }
}