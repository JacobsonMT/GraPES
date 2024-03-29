$(document).ready(function () {

  $('#precomputed-table').DataTable({
    deferRender:    true,
    ajax: {
      url: '/api/precomputed/datatable',
      // dataSrc: '',
      data: function (data) {
        return JSON.stringify(data);
      },
      processData: false,
      dataType: "json",
      contentType: "application/json;charset=UTF-8",
      type: "POST",
      error: function (xhr, error, code)
      {
        console.log(xhr);
        console.log(code);
      }
    },
    pagingType: "simple_numbers",
    "bInfo": false,
    searchDelay: 400,
    "processing": true,
    "serverSide": true,
    // Move the search bar
    dom: "<'row'<'col-lg-10 col-md-10 col-xs-12'f><'col-lg-2 col-md-2 col-xs-12'l>>" +
        "<'row'<'col-sm-12'tr>>" +
        "<'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",
    columnDefs: [
      {
        targets: 0,
        className: 'text-left mono-font-body',
        data: "accession",
        render: function ( data, type, row, meta ) {
          return '<span class="align-middle">' +
              '<div style="display: flex; flex-wrap: wrap; align-items: center; justify-content: left;">' +
              '<a href="//www.uniprot.org/uniprot/' + data + '" target="_blank" class="mr-2 uniprot-url"></a>' +
              data +
              '</div>' +
              '</span>';
        }
      },
      {
        targets: 1,
        data: "gene",
        className: 'text-right mono-font-body'
      },
      {
        targets: 2,
        data: "species",
        className: 'text-right mono-font-body'
      },
      {
        targets: 3,
        data: "score",
        className: 'text-right mono-font-body',
        render: function (data, type, row, meta) {
          return parseFloat(data).toFixed(2)
        }
      },
      {
        targets: 4,
        data: "accession",
        searchable: false,
        orderable: false,
        className: 'text-center',
        render: function ( data, type, row, meta ) {
          return '<span class="align-middle">' +
              '<a href="/precomputed/' + data + '" target="_blank" class="align-middle mr-2"><i class="fas fa-chart-area mr-1"></i>View</a>' +
              '<a href="/api/precomputed/' + data + '/csv" target="_blank" class="align-middle mr-2"><i class="fas fa-file-csv mr-1"></i>CSV</a>' +
              '<a href="/api/precomputed/' + data + '" target="_blank" class="align-middle"><i class="fas fa-file-code mr-1"></i>JSON</a>' +
              '</span>';
        }
      }
    ]
  });

  var dtable = $('#precomputed-table').dataTable().api();
  var searchWait = 0;
  var searchWaitInterval;
  $('.dataTables_filter input')
  .unbind() // Unbind previous default bindings
  .bind("input", function(e) { // Bind our desired behavior
    var item = $(this);
    searchWait = 0;

    if($(item).val() == "") {
      dtable.search("").draw();
      return;
    }

    if(!searchWaitInterval) searchWaitInterval = setInterval(function(){
      if(searchWait>=3){
        clearInterval(searchWaitInterval);
        searchWaitInterval = '';
        searchTerm = $(item).val();
        dtable.search(searchTerm).draw();
        searchWait = 0;
      }
      searchWait++;
    },200);

  });

});