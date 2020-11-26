package server;

import utils.AuthCard;

public interface AuthService {
    public AuthCard add(AuthCard authCard);
    public AuthCard login(AuthCard authCard);
    public String check(Integer token);
}
