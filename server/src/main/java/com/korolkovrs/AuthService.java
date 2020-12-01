package com.korolkovrs;

import java.io.IOException;
import java.sql.SQLException;

public interface AuthService {
    public AuthCard add(AuthCard authCard) throws SQLException, IOException;
    public AuthCard login(AuthCard authCard) throws SQLException;
    public String check(Integer token);
}
