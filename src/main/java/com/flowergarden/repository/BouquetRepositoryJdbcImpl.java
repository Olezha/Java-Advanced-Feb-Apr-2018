package com.flowergarden.repository;

import com.flowergarden.model.bouquet.Bouquet;
import com.flowergarden.model.bouquet.MarriedBouquet;
import com.flowergarden.model.flowers.Flower;
import com.flowergarden.sql.JdbcConnectionPool;
import com.flowergarden.sql.SqlStatements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class BouquetRepositoryJdbcImpl implements BouquetRepository {

    private JdbcConnectionPool connectionPool;

    private FlowerRepository flowerRepository;

    private SqlStatements sql;

    @Autowired
    public BouquetRepositoryJdbcImpl(
            JdbcConnectionPool connectionPool,
            FlowerRepository flowerRepository,
            SqlStatements sql) {
        this.connectionPool = connectionPool;
        this.flowerRepository = flowerRepository;
        this.sql = sql;
    }

    @Override
    public Bouquet saveOrUpdate(Bouquet bouquet) throws SQLException {
        try (Connection connection = connectionPool.getConnection()) {
            if (bouquet.getId() == null) {
                try (PreparedStatement statement = connection.prepareStatement(sql.get("BOUQUET_SAVE"))) {
                    // TODO
                    statement.setString(1, "married");
                    statement.setBigDecimal(2, bouquet.getAssemblePrice());
                    statement.executeUpdate();
                    bouquet.setId(statement.getGeneratedKeys().getInt(1));
                }
            }
            else {
                try (PreparedStatement statement = connection.prepareStatement(sql.get("BOUQUET_UPDATE"))) {
                    statement.setBigDecimal(1, bouquet.getAssemblePrice());
                    statement.setInt(2, bouquet.getId());
                    statement.executeUpdate();
                }
            }
        }
        flowerRepository.saveOrUpdateBouquetFlowers(bouquet.getFlowers(), bouquet.getId());
        return bouquet;
    }

    @Override
    public Bouquet<Flower> findOne(int id) throws SQLException {
        List<Bouquet<Flower>> bouquets;
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.get("BOUQUET_FIND_ONE"))) {
            statement.setInt(1, id);
            bouquets = convert(statement.executeQuery());
        }

        if (bouquets.isEmpty())
            return null;

        if (bouquets.size() > 1)
            throw new SQLIntegrityConstraintViolationException("Unique id");

        return new LazyBouquet(flowerRepository, bouquets.get(0));
    }

    @Override
    public Iterable<Bouquet<Flower>> findAll() throws SQLException {
        try (Connection connection = connectionPool.getConnection();
             Statement statement = connection.createStatement()) {
            return convert(statement.executeQuery(sql.get("BOUQUET_FIND_ALL")));
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        flowerRepository.deleteBouquetFlowers(id);
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.get("BOUQUET_DELETE"))) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Bouquet bouquet) throws SQLException {
        if (bouquet.getId() == null)
            throw new IllegalArgumentException("Not persisted entity");
        delete(bouquet.getId());
    }

    @Override
    public void deleteAll() throws SQLException {
        flowerRepository.deleteAll();
        try (Connection connection = connectionPool.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql.get("BOUQUET_DELETE_ALL"));
        }
    }

    @Override
    public BigDecimal getBouquetPrice(int bouquetId) throws SQLException {
        // TODO: change db money columns to INTEGER
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.get("BOUQUET_PRICE"))) {
            statement.setInt(1, bouquetId);
            return new BigDecimal(statement.executeQuery().getString(1));
        }
    }

    private List<Bouquet<Flower>> convert(ResultSet resultSet) throws SQLException {
        List<Bouquet<Flower>> bouquets = new ArrayList<>();
        while (resultSet.next()) {
            Bouquet<Flower> bouquet = new MarriedBouquet();
            bouquet.setId(resultSet.getInt("id"));
            bouquet.setAssemblePrice(new BigDecimal(resultSet.getString("assemble_price")));
            bouquets.add(bouquet);
        }
        return bouquets;
    }
}

class LazyBouquet implements Bouquet<Flower> {

    private FlowerRepository flowerRepository;

    private Bouquet<Flower> bouquet;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    LazyBouquet(FlowerRepository flowerRepository, Bouquet<Flower> bouquet) {
        this.flowerRepository = flowerRepository;
        this.bouquet = bouquet;
    }

    private void loadFlowers() {
        try {
            for (Flower flower : flowerRepository.findBouquetFlowers(bouquet.getId()))
                bouquet.addFlower(flower);
        } catch (SQLException e) {
            log.error("{}", e);
        }
    }

    @Override
    public BigDecimal getPrice() {
        if (bouquet.getFlowers().isEmpty())
            loadFlowers();
        return bouquet.getPrice();
    }

    @Override
    public void addFlower(Flower flower) {
        if (bouquet.getFlowers().isEmpty())
            loadFlowers();
        bouquet.addFlower(flower);
    }

    @Override
    public Collection<Flower> searchFlowersByLength(int start, int end) {
        if (bouquet.getFlowers().isEmpty())
            loadFlowers();
        return bouquet.searchFlowersByLength(start, end);
    }

    @Override
    public void sortByFreshness() {
        if (bouquet.getFlowers().isEmpty())
            loadFlowers();
        bouquet.sortByFreshness();
    }

    @Override
    public Collection<Flower> getFlowers() {
        if (bouquet.getFlowers().isEmpty())
            loadFlowers();
        return bouquet.getFlowers();
    }

    @Override
    public String getName() {
        return bouquet.getName();
    }

    @Override
    public void setAssemblePrice(BigDecimal assemblePrice) {
        bouquet.setAssemblePrice(assemblePrice);
    }

    @Override
    public Integer getId() {
        return bouquet.getId();
    }

    @Override
    public void setId(Integer id) {
        bouquet.setId(id);
    }

    @Override
    public BigDecimal getAssemblePrice() {
        return bouquet.getAssemblePrice();
    }
}
