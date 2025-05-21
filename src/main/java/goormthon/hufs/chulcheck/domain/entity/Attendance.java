package goormthon.hufs.chulcheck.domain.entity;

import java.time.LocalDateTime;

import goormthon.hufs.chulcheck.domain.enums.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(
	name = "attendance",
	uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "session_id"})
)
@Entity
@Data
@NoArgsConstructor
public class Attendance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "session_id", nullable = false)
	private AttendanceSession attendanceSession;

	@Column(name = "attendance_time")
	private LocalDateTime attendanceTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private AttendanceStatus status;

	@Builder
	public Attendance(User user, AttendanceSession attendanceSession) {
		this.user = user;
		this.attendanceSession = attendanceSession;
	}
}
