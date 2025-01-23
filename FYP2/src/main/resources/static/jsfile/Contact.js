function isValidName(Name) {
			// Define the regular expression pattern for a valid name
			const namePattern = /^[A-Z][a-z]+$/;

			// Test the input name against the pattern
			return namePattern.test(Name);
		}

		// Function to validate email format
		function validateEmail(email) {
			// Regular expression pattern for email validation
			var pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
			return pattern.test(email);
		}

		function isValidPhoneNumber(Checktel) {
			// Check if the phone number has exactly 8 digits and starts with 8 or 9
			return /^[89]\d{7}$/.test(Checktel);
		}

		function checkCredentials() {
			var NameInput = document.getElementsByName('Name')[0].value;
			var emailInput = document.getElementsByName('email')[0].value;
			var ContactInput = document.getElementsByName('Checktel')[0].value;
			var SubjectInput = document.getElementsByName('Subject')[0].value;

			if (NameInput.trim() === '' || emailInput.trim() === ''
					|| ContactInput.trim() === '' || SubjectInput.trim() === '') {
				alert('Please enter all fields.');
				return;
			}

			if (!isValidName(NameInput)) {
				alert('Please enter a valid Name (Start with an uppercase letter)');
				return;
			}

			if (!validateEmail(emailInput)) {
				alert('Invalid email format.');
				return;
			}

			if (!isValidPhoneNumber(ContactInput)) {
				alert('Please enter a valid 8-digit phone number that starts with 8 or 9.');
				return;
			}

			alert('Submit successful, thank you for your feedback!');
			document.getElementsByName('Name')[0].value = '';
			document.getElementsByName('email')[0].value = '';
			document.getElementsByName('Checktel')[0].value = '';
			document.getElementsByName('Subject')[0].value = '';
		}

		var SubmitButton = document.querySelector('.submit');
		SubmitButton.addEventListener('click', checkCredentials);
		
function initMap() {
            // Location coordinates for 9 Woodlands Ave 9, Singapore
            var location = { lat: 1.444743, lng: 103.784982 };

            // Create a map centered at the specified location
            var map = new google.maps.Map(document.getElementById('map'), {
                center: location,
                zoom: 15 // Adjust the zoom level as needed
            });

            // Create a marker at the specified location
            var marker = new google.maps.Marker({
                position: location,
                map: map,
                title: '9 Woodlands Ave 9, Singapore 738964'
            });
        }