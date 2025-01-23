function validateForm() {
            var questions = document.querySelectorAll('[name^="feedback"]');
            
            for (var i = 0; i < questions.length; i++) {
                var question = questions[i].name;
                var selectedResponse = document.querySelector('[name="' + question + '"]:checked');

                if (!selectedResponse) {
                    alert('Please answer all questions before submitting.');
                    return false;
                }
            }

            // If all questions have a selected response, show a thank you message
            alert('Thank you for your feedback!');
            return true;
        }