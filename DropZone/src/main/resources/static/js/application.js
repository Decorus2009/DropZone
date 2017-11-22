$(document).ready(function () {
    /**
     * https://github.com/puleos/object-hash
     * map: file -> сгенерированный sha1 hash при помощи object-hash
     * Хэши нужны для уникальной идентификации отправляемых на сервер файлов
     * и инициализации id='progress_<hash>' прогресс баров в сгенерированных формах для файлов
     * @type {Map}
     */
    const hashes = new Map();

    // "dropzoneForm" is the camel-case version of the form id "dropzone-form"
    Dropzone.options.dropzoneForm = {
        autoProcessQueue: true, // true - файлы начнут загружаться автоматически
        uploadMultiple: false, // было true
        maxFilesize: 500, // MB
        parallelUploads: 100,
        maxFiles: 1000,
        addRemoveLinks: false,
        // previewsContainer: ".dropzone-previews",

        // The setting up of the dropzone
        init: function () {

            const myDropzone = this;

            /**
             * http://www.dropzonejs.com/#tips
             * отправка дополнительной информации вместе с файлом (в данном случае, hash)
             * на сервере этот хэш понадобится для определения прогресса в загрузке такого-то файла на Яндекс.Диск
             */
            this.on("addedfile", function (file) {
                /**
                 * objectHash.sha1(file) - ругается, что file - не тот тип.
                 * Поэтому строится новый объект из пропертей файла, от которого хэш взять можно
                 */
                const hash = computeHash(file);
                hashes.set(file, hash);

                const oldContent = '<span class="dz-upload" data-dz-uploadprogress';
                const newContent = '<span id="' + hash + '" class="dz-upload" data-dz-uploadprogress';
                file.previewElement.innerHTML = file.previewElement.innerHTML.replace(oldContent, newContent);


                // Create the remove button
                const removeButton = Dropzone.createElement("<button>Remove file</button>");
                // Capture the Dropzone instance as closure.
                const _this = this;
                // Listen to the click event
                removeButton.addEventListener("click", function(e) {
                    console.log("CLICKED");
                    // Make sure the button click doesn't submit the form:
                    e.preventDefault();
                    e.stopPropagation();
                    // Remove the file preview.
                    _this.removeFile(file);

                    // TODO удаление файла из Яндекс.Диска

                    // If you want to the delete the file on the server as well,
                    // you can do the AJAX request here.
                });
                // Add the button to the file preview element.
                file.previewElement.appendChild(removeButton);
            });

            this.on("sending", function (file, xhr, formData) {
                formData.append("hash", hashes.get(file));
            });

            // customizing the default progress bar
            this.on("uploadprogress", function (file, progress) {
                progress = parseFloat(progress).toFixed(0) / 2;
                updateUploadProgressOf(file, progress);

                /**
                 * При достижении значения progress 50, т.е., когда файл отправлен на сервер,
                 * начинается периодический опрос сервера для получения прогресса загрузки файла в облако
                 */
                if (progress == '50') {
                    console.log("REACHED 50");
                    runPeriodicProgressUpdateRequests(file);
                }
            });

            this.on("success", function (file) {
            });

            this.on("error", function (file, errorMessage, xhr) {
                // $(file.previewElement).find('.dz-upload').css("width", "100%");
                // $(file.previewElement).find('.dz-upload').text("ERROR");
                // $(file.previewElement).find('.dz-upload').css("background-color", "red");

                let message;
                if (xhr.status === 409 || xhr.status === 413) {
                    message = xhr.responseText;
                } else {
                    message = "Connection failed";
                }

                $(file.previewElement).find('.dz-error-message').text(message);


                // errorMessage = xhr.responseText;
                //
                // console.log("errror");
                // console.log(xhr.status);
                // console.log(xhr.responseText);
                // console.log(errorMessage);
                // console.log(xhr);
                // console.log();
            });
        }
    };


    // TODO период запроса в зависимости от размера файла
    function runPeriodicProgressUpdateRequests(file) {
        /**
         * https://github.com/RobertFischer/JQuery-PeriodicalUpdater
         * Оборачивает периодические асинхронные ajax-вызовы
         */
        $.PeriodicalUpdater('/some_path', {
            url: 'http://localhost:8080/progress',
            cache: false,     // By default, don't allow caching
            method: 'POST',    // method; get or post
            data: {
                hash: hashes.get(file)
            },         // array of values to be passed to the page - e.g. {name: "John", greeting: "hello"}
            minTimeout: 100, // starting value for the timeout in milliseconds
            maxTimeout: 100, // maximum length of time between requests
            multiplier: 1,    // if set to 2, timerInterval will double each time the response hasn't changed (up to maxTimeout)
            maxCalls: 0,      // maximum number of calls. 0 = no limit.
            maxCallsCallback: null, // The callback to execute when we reach our max number of calls
            autoStop: 0,      // automatically stop requests after this many returns of the same data. 0 = disabled
            autoStopCallback: null, // The callback to execute when we autoStop
            cookie: false,    // whether (and how) to store a cookie
            runatonce: false, // Whether to fire initially or wait
            verbose: 0,        // The level to be logging at: 0 = none; 1 = some; 2 = all

        }, function (data, success, xhr, handle) {
            // console.log(data);
            // console.log(xhr.status);
            // console.log(xhr);
            // console.log();

            /**
             * Весь этот асинхронный вызов по посылке запросов на сервер по идее запускается сразу в процессе отправки.
             * На сервере еще может не быть информации о том, что к нему летит такой-то файл
             * (тем более, что у него такой-то хэш и столько-то процентов загружено на Яндекс.Диск)
             *
             * Не похоже, что эта штука корректно работает, все равно data == null или undefined пролезает
             */
            if (data) {
                console.log(data);
                /**
                 * меняем html контент, привязанный к данному файлу (progress bar и текст над ним)
                 */
                updateUploadProgressOf(file, parseInt(data, 10) + 50);

                // $('#progress_' + hashes.get(file)).val(data);
                // $(`#status_${hashes.get(file)}`).text(data + '%');
                //
                if (data === '50') {
                    // $(file.previewElement).find('.dz-upload').text("SUCCESS");
                    handle.stop();
                }
            }
        });
    }

    function updateUploadProgressOf(file, progress) {
        const hash = hashes.get(file);
        $("#" + hash).attr("style", "width: " + progress + "%");
        // $("#" + hash).html(progress + "%");
    }

    function computeHash(file) {
        return objectHash.sha1({
            lastModified: file.lastModified,
            lastModifiedDate: file.lastModifiedDate,
            name: file.name,
            size: file.size,
            type: file.type,
            date: Date.now()
        });
    }

    function showInformationDialog(files, objectArray) {

        var responseContent = "Upload complete";

        for (var i = 0; i < objectArray.length; i++) {

            var infoObject = objectArray[i];

            for (var infoKey in infoObject) {
                if (infoObject.hasOwnProperty(infoKey)) {
                    responseContent = responseContent + " " + infoKey + " -> " + infoObject[infoKey] + "<br>";
                }
            }
            responseContent = responseContent + "<hr>";
        }

        // from the library bootstrap-dialog.min.js
        BootstrapDialog.show({
            title: '<b>Server Response</b>',
            message: responseContent
        });
    }
});