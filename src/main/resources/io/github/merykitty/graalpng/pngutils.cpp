#include <vector>
#include <iostream>
#include <polyglot.h>
#include <png.h>

static_assert(sizeof(int) == 4);

extern "C" {
int png_read(char* file_name, int file_name_length, int width, int height, int* data, bool big_endian) {
    std::string _file_name(file_name_length, '\0');
    for (auto i = 0; i < file_name_length; i++) {
        _file_name[i] = file_name[i];
    }
    auto _fp = fopen(_file_name.c_str(), "rb");
    if (_fp == nullptr) {
        return -1;
    }
    auto _png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
    if (_png_ptr == nullptr) {
        fclose(_fp);
        return -1;
    }
    auto _info_ptr = png_create_info_struct(_png_ptr);
    if (_info_ptr == nullptr) {
        png_destroy_read_struct(&_png_ptr, nullptr, nullptr);
        fclose(_fp);
        return -1;
    }
    if (setjmp(png_jmpbuf(_png_ptr))) {
        png_destroy_read_struct(&_png_ptr, &_info_ptr, nullptr);
        fclose(_fp);
        return -1;
    }
    png_init_io(_png_ptr, _fp);
    std::vector<int> _data(width * height);
    std::vector<png_bytep> _row_ptrs(height);
    for (auto i = 0; i < height; i++) {
        _row_ptrs[i] = (png_bytep)&_data[i * width];
    }
    png_set_rows(_png_ptr, _info_ptr, _row_ptrs.data());
    int _transform;
    if (big_endian) {
        _transform = PNG_TRANSFORM_SWAP_ALPHA;
    } else {
        _transform = PNG_TRANSFORM_BGR;
    }
    png_read_png(_png_ptr, _info_ptr, _transform, nullptr);
    png_destroy_read_struct(&_png_ptr, &_info_ptr, nullptr);
    fclose(_fp);
    for (auto i = 0; i < width * height; i++) {
        data[i] = _data[i];
    }
    return 0;
}

int png_write(char* file_name, int file_name_length, int width, int height, int* data, bool big_endian) {
    std::string _file_name(file_name_length, '\0');
    for (auto i = 0; i < file_name_length; i++) {
        _file_name[i] = file_name[i];
    }
    auto _fp = fopen(_file_name.c_str(), "wb");
    if (_fp == nullptr) {
        return -1;
    }
    auto _png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
    if (_png_ptr == nullptr) {
        fclose(_fp);
        return -1;
    }
    auto _info_ptr = png_create_info_struct(_png_ptr);
    if (_info_ptr == nullptr) {
        png_destroy_write_struct(&_png_ptr, nullptr);
        fclose(_fp);
        return -1;
    }
    if (setjmp(png_jmpbuf(_png_ptr))) {
        png_destroy_write_struct(&_png_ptr, &_info_ptr);
        fclose(_fp);
        return -1;
    }
    png_init_io(_png_ptr, _fp);
    png_set_IHDR(_png_ptr, _info_ptr, width, height, 8, PNG_COLOR_TYPE_RGBA, PNG_INTERLACE_NONE, PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);
    std::vector<int> _data(width * height);
    for (auto i = 0; i < width * height; i++) {
        _data[i] = data[i];
    }
    std::vector<png_bytep> _row_ptrs(height);
    for (auto i = 0; i < height; i++) {
        _row_ptrs[i] = (png_bytep)&_data[i * width];
    }
    png_set_rows(_png_ptr, _info_ptr, _row_ptrs.data());
    int _transform;
    if (big_endian) {
        _transform = PNG_TRANSFORM_SWAP_ALPHA;
    } else {
        _transform = PNG_TRANSFORM_BGR;
    }
    png_write_png(_png_ptr, _info_ptr, _transform, nullptr);
    png_destroy_write_struct(&_png_ptr, &_info_ptr);
    fclose(_fp);
    return 0;
}
}