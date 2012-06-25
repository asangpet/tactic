function [ output_args ] = deconv_wnr( a, b )
    len = max(size(a));
    temp = deconvwnr(a,b);
    output_args = circshift(temp, [0, -len/2]);
end

