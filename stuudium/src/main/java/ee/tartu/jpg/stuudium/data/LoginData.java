package ee.tartu.jpg.stuudium.data;

import ee.tartu.jpg.stuudium.data.upper.StuudiumData;

public class LoginData extends StuudiumData {

    private String access_token;

    private long expires_in;

    private long request_time;

    public LoginData(String access_token, long expires_in) {
        this.access_token = access_token;
        this.expires_in = expires_in;
        this.request_time = System.currentTimeMillis() / 1000;
    }

    public LoginData(String access_token, long expires_in, long request_time) {
        this.access_token = access_token;
        this.expires_in = expires_in;
        this.request_time = request_time;
    }

    public String getAccessToken() {
        return access_token;
    }

    public long getExpiresIn() {
        return expires_in;
    }

    public long getRequestTime() {
        return request_time;
    }

    @Override
    public int compareTo(StuudiumData another) {
        return 0;
    }

    @Override
    public int hashCode() {
        return access_token.hashCode();
    }
}
