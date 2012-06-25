function [ output_args ] = deconv_lucy( a, b )
    len = max(size(a));
    temp = deconvlucy(a,b);
    output_args = circshift(temp, [0, -len/2]);
end

