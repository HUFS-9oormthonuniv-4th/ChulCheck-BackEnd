package goormthon.hufs.chulcheck.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClubRequest {
    private String name;
    private String representativeAlias;
    private String memberAlias;
    private String description;
    private String ownerId;
}