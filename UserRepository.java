/*Модуль для работы с БД*/
package com.vsu.app.repository;

import com.vsu.app.entity.User;
import com.vsu.app.exception.DBException;
import com.vsu.app.exception.RecordNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class UserRepository {
    private static final Logger LOGGER = Logger.getLogger(UserRepository.class.getName());

    private static final String SELECT_BY_ID_QUERY =
            "SELECT * FROM profile WHERE id_user = ?";

    private static final String SELECT_BY_EMAIL_QUERY =
            "SELECT * FROM profile WHERE email_user = ?";

    private static final String INSERT_QUERY =
            "INSERT INTO profile(" +
                    "surname_user, name_user, birthday_user, phone_user, email_user, password_user) " +
                    "VALUES (?, ?, ?, ?, ?, ?);";

    private static final String DELETE_QUERY_BY_ID =
            "DELETE FROM profile WHERE id_user = ?";

    private static final String UPDATE_QUERY =
            "UPDATE profile " +
                    "SET surname_user=?, name_user=?, birthday_user=?, phone_user=?, email_user=?, password_user=? " +
                    "WHERE id_user = ?";

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User getById(Long notId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY, (rs, rowNum) -> getUserByResultSet(rs), id);
        } catch (DataAccessException e) {
            LOGGER.log(Level.WARNING, "User with id {0} is not found", id);
            throw new RecordNotFoundException("User is not found");
        }
    }

    public User getByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(SELECT_BY_EMAIL_QUERY, (rs, rowNum) -> getUserByResultSet(rs), email);
        } catch (DataAccessException e) {
            LOGGER.log(Level.WARNING, "User with email {0} is not found", email);
            throw new RecordNotFoundException("User is not found");
        }
    }

    public User insert(User user) {
        try {
            SimpleJdbcInsert insertContactList = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("profile").usingColumns("surname_user", "name_user", "birthday_user",
                            "phone_user", "email_user", "password_user")
                    .usingGeneratedKeyColumns("id_user");

            return getById((Long) insertContactList.executeAndReturnKey(getInsertParams(user)));
        } catch (DataAccessException e) {
            LOGGER.log(Level.WARNING, "User with email {0} is not inserted", user.getEmail());
            throw new DBException(e);
        }
    }

    public int updateById(User user) {
        try {
            return jdbcTemplate.update(UPDATE_QUERY, user.getSurname(), user.getName(), Date.valueOf(user.getBirthday()),
                    user.getPhone(), user.getEmail(), user.getPassword(), user.getId());
        } catch (DataAccessException e) {
            LOGGER.log(Level.WARNING, "User with id {0} is not updated", user.getId());
            throw new DBException(e);
        }
    }

    public int deleteById(Long id) {
        try {
            return jdbcTemplate.update(DELETE_QUERY_BY_ID, id);
        } catch (DataAccessException e) {
            LOGGER.log(Level.WARNING, "User with id {0} is not deleted", id);
            throw new DBException(e);
        }

    }

    private User getUserByResultSet(ResultSet rs) throws SQLException {
        return new User(rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getDate(4).toString(),
                rs.getString(6),
                rs.getString(5),
                rs.getString(7));
    }


    private static Map<String, Object> getInsertParams(User user) {
        Map<String, Object> insertParameters = new HashMap<>();
        insertParameters.put("surname_user", user.getSurname());
        insertParameters.put("name_user", user.getName());
        insertParameters.put("birthday_user", Date.valueOf(user.getBirthday()));
        insertParameters.put("phone_user", user.getPhone());
        insertParameters.put("email_user", user.getEmail());
        insertParameters.put("password_user", user.getPassword());
        return insertParameters;
    }
}
