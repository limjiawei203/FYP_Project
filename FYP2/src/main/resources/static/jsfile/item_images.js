function displaySelectedImage() {
            // Get the file input element
            var input = document.getElementById('itemImage');

            // Get the selected file
            var file = input.files[0];

            // Check if a file is selected
            if (file) {
                // Create a FileReader to read the image
                var reader = new FileReader();

                // Set the function to be called when the FileReader has finished reading the file
                reader.onload = function (e) {
                    // Get the data URL of the image
                    var imageDataUrl = e.target.result;

                    // Display the image
                    var imageContainer = document.getElementById('imageContainer');
                    imageContainer.innerHTML = '<img src="' + imageDataUrl + '" alt="Selected Image" style="max-width: 100%; max-height: 200px;">';
                };

                // Read the image file as a data URL
                reader.readAsDataURL(file);
            }
        }