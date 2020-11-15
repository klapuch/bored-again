document.addEventListener('DOMContentLoaded', function (event) {
    const tagIcons = document.getElementsByTagName('a');
    const tagInput = document.getElementById('tags');

    for (let i = 0; i < tagIcons.length; i++) {
        let tagIcon = tagIcons[i];
        if (Array.from(tagIcon.classList).includes('tag')) {
            tagIcon.onclick = (event) => {
                event.preventDefault();
                const prefix = tagInput.value.length === 0 ? '' : ',';
                tagInput.value = tagInput.value + prefix + tagIcon.innerHTML;
            };
        }
    }
});
