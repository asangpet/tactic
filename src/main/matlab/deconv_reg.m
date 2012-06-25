function [ output_args ] = deconv_reg( a, b )
    len = max(size(a));
    temp = deconvreg(a,b);
    output_args = circshift(temp, [0, -len/2]);
end

