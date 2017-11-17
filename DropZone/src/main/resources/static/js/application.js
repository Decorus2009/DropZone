$(document).ready(function () {
    /**
     * https://github.com/puleos/object-hash
     * map: file -> сгенерированный sha1 hash при помощи object-hash
     * Хэши нужны для уникальной идентификации отправляемых на сервер файлов
     * и инициализации id='progress_<hash>' прогресс баров в сгенерированных формах для файлов
     * @type {Map}
     */
    const hashes = new Map();


    $(".file-dropzone").on('dragover', handleDragEnter);
    $(".file-dropzone").on('dragleave', handleDragLeave);
    $(".file-dropzone").on('drop', handleDragLeave);

    function handleDragEnter(e) {
        this.classList.add('drag-over');
    }

    function handleDragLeave(e) {
        this.classList.remove('drag-over');
    }

    // "dropzoneForm" is the camel-case version of the form id "dropzone-form"
    Dropzone.options.dropzoneForm = {
        autoProcessQueue: true, // true - файлы начнут загружаться автоматически
        uploadMultiple: false, // было true
        maxFilesize: 1000, // MB
        parallelUploads: 100,
        maxFiles: 100,
        addRemoveLinks: true,
        previewsContainer: ".dropzone-previews",

        // The setting up of the dropzone
        init: function () {

            const myDropzone = this;

            // // first set autoProcessQueue = false
            // $('#upload-button').on("click", function (e) {
            //     myDropzone.processQueue();
            // });

            /**
             * http://www.dropzonejs.com/#tips
             * отправка дополнительной информации вместе с файлом (в данном случае, hash)
             * на сервере этот хэш понадобится для определения прогресса в загрузке такого-то файла на Яндекс.Диск
             */
            this.on("addedfile", function (file) {
                console.log("ADDED");
                /**
                 * objectHash.sha1(file) - ругается, что file - не тот тип.
                 * Поэтому строится новый объект из пропертей файла, от которого хэш взять можно
                 */
                const hash = objectHash.sha1({
                    lastModified: file.lastModified,
                    lastModifiedDate: file.lastModifiedDate,
                    name: file.name,
                    size: file.size,
                    type: file.type
                });
                hashes.set(file, hash);

                /**
                 * При добавлении файла генерируется определенный html-контент (http://www.dropzonejs.com/#layout),
                 * который содержит div, отвечающий за dropzonejs progress bar.
                 * Устанавливать значения для него более проблематично, чем заменить его на собственный progress bar
                 * и явно устанавливать значение шкалы прогресса через периодические асинхронные вызовы к серверу,
                 * возвращающие прогресс в отношении загрузки файла ИМЕННО на Яндкес.Диск, а не просто на сервер.
                 */
                const oldContent = '<div class="dz-progress"><span class="dz-upload" data-dz-uploadprogress=""></span></div>';
                const newContent =
                    '<div>' + '\n' +
                    '<div>' + '\n' +
                    '<p id="status_' + hash + '">Preparing for upload</p>' + '\n' +
                    '</div>' + '\n' +

                    '<div>' + '\n' +
                    '<progress id="progress_' + hash + '" value="0" max="100">Текст</progress>' + '\n' +
                    '</div>' + '\n' +
                    '</div>';

                file.previewElement.innerHTML = file.previewElement.innerHTML.replace(oldContent, newContent);
            });

            this.on("sending", function (file, xhr, formData) {
                console.log("SENDING");
                formData.append("hash", hashes.get(file));
                /**
                 * https://github.com/RobertFischer/JQuery-PeriodicalUpdater
                 * Оборачивает периодические асинхронные ajax-вызовы
                 * @type {number}
                 */

                // TODO период запроса в зависимости от размера файла

                $.PeriodicalUpdater('/some_path', {
                    url: 'http://localhost:8080/progress',
                    cache: false,     // By default, don't allow caching
                    method: 'POST',    // method; get or post
                    data: {
                        hash: hashes.get(file)
                    },         // array of values to be passed to the page - e.g. {name: "John", greeting: "hello"}
                    minTimeout: 50, // starting value for the timeout in milliseconds
                    maxTimeout: 50, // maximum length of time between requests
                    multiplier: 1,    // if set to 2, timerInterval will double each time the response hasn't changed (up to maxTimeout)
                    maxCalls: 0,      // maximum number of calls. 0 = no limit.
                    maxCallsCallback: null, // The callback to execute when we reach our max number of calls
                    autoStop: 0,      // automatically stop requests after this many returns of the same data. 0 = disabled
                    autoStopCallback: null, // The callback to execute when we autoStop
                    cookie: false,    // whether (and how) to store a cookie
                    runatonce: false, // Whether to fire initially or wait
                    verbose: 0,        // The level to be logging at: 0 = none; 1 = some; 2 = all

                }, function (data, success, xhr, handle) {
                    /**
                     * Весь этот асинхронный вызов по посылке запросов на сервер по идее запускается сразу в процессе отправки.
                     * На сервере еще может не быть информации о том, что к нему летит такой-то файл
                     * (тем более, что у него такой-то хэш и столько-то процентов загружено на Яндекс.Диск)
                     *
                     * Не похоже, что эта штука корректно работает, все равно data == null или undefined пролезает
                     */
                    if (data) {
                        if (data) {
                            console.log(data);

                            /**
                             * меняем html контент, привязанный к данному файлу (progress bar и текст над ним)
                             */
                            $('#progress_' + hashes.get(file)).val(data);
                            $(`#status_${hashes.get(file)}`).text(data + '%');

                            if (data === '100') {
                                $(`#status_${hashes.get(file)}`).text('Upload complete');
                                handle.stop();
                            }
                        }
                    }
                });

            });

            // // customizing the default progress bar
            // this.on("uploadprogress", function (file, progress) {
            //     // progress = parseFloat(progress).toFixed(0);
            //     //
            //     // var progressBar = file.previewElement.getElementsByClassName("dz-upload")[0];
            //     // progressBar.innerHTML = progress + "%";
            // });

            // displaying the uploaded files information in a Bootstrap dialog
            // this.on("successmultiple", function (files, serverResponse) {
            //     BootstrapDialog.show({
            //         title: '<b>Server Response</b>',
            //         message: 'SUCCESS'
            //     });
            //     // console.log("SUCCESS");
            //     // showInformationDialog(files, serverResponse);
            // });

            /**
             * Вызывается в случае успешной отправки каждого файла
             */
            this.on("success", function (file) {
            });
        }
    };

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