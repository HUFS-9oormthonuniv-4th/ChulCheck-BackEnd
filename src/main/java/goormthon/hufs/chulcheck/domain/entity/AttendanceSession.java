package goormthon.hufs.chulcheck.domain.entity;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
public class AttendanceSession {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String sessionName;
	private String description;
	private String place;
	private Time startTime;
	private Time endTime;
	private LocalDateTime sessionDate;

	@ManyToOne
	@JoinColumn(name = "club_id")
	private Club club;

	@OneToMany(mappedBy = "attendanceSession", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Attendance> attendanceList;
}
