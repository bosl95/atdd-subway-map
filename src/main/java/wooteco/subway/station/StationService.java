package wooteco.subway.station;

import org.springframework.stereotype.Service;
import wooteco.subway.station.dao.StationDao;
import wooteco.subway.station.domain.Station;

import java.util.List;

@Service
public class StationService {

    private final StationDao stationDao;

    public StationService(StationDao stationDao) {
        this.stationDao = stationDao;
    }

    public Station save(Station station) {
        if (stationDao.countStationByName(station.getName()) > 0) {
            throw new IllegalArgumentException("중복된 지하철 역입니다.");
        }
        Long id = stationDao.save(station);
        return new Station(id, station.getName());
    }

    public List<Station> findAll() {
        return stationDao.findAll();
    }

    public void delete(Long id) {
        stationDao.delete(id);
    }
}