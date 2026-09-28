$(document).ready(function () {

  // ---- Trang profile: gọi API được bảo vệ kèm Bearer token ----
  if ($('#profile').length) {
    $.ajax({
      type: 'GET',
      url: '/users/me',
      dataType: 'json',
      beforeSend: function (xhr) {
        var token = localStorage.getItem('token');
        if (token) {
          xhr.setRequestHeader('Authorization', 'Bearer ' + token);
        }
      },
      success: function (data) {
        // .text() thay vì .html(): fullName do người dùng nhập, dùng .html() là mở đường cho XSS
        $('#profile').text(data.fullName);
        if (data.images) {
          $('#images').attr('src', data.images);
        }
      },
      error: function () {
        alert('Sorry, you are not logged in.');
        window.location.href = '/login';
      }
    });
  }

  // ---- Đăng xuất (chỉ xoá token phía client; server không thu hồi được token) ----
  $('#logout').click(function () {
    localStorage.removeItem('token');
    window.location.href = '/login';
  });

  // ---- Đăng nhập ----
  $('#Login').click(function () {
    var basicInfo = JSON.stringify({
      email: $('#email').val(),
      password: $('#password').val()
    });
    $.ajax({
      type: 'POST',
      url: '/auth/login',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      data: basicInfo,
      success: function (data) {
        // Theo slide: lưu localStorage. Đánh đổi: XSS đọc được token.
        localStorage.setItem('token', data.token);
        window.location.href = '/user/profile';
      },
      error: function () {
        alert('Login Failed');
      }
    });
  });
});
