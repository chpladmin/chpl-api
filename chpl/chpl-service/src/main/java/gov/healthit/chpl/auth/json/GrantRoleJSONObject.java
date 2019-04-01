package gov.healthit.chpl.auth.json;

public class GrantRoleJSONObject {
		
		private String subjectName;
		private String role;

		public String getSubjectName() {
			return subjectName;
		}

		public void setSubjectName(String subjectName) {
			this.subjectName = subjectName;
		}

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}
		
}
