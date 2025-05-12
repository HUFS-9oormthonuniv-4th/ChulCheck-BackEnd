package goormthon.hufs.chulcheck.domain.dto;

public interface OAuth2Response {
    String getProvider();

    String getProviderId();

    String getNickname();

    String getImage();
}