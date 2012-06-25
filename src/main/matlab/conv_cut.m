function [ output_args ] = conv_cut( a, b )
    len = max(size(a));
    conv_result = conv(a,b);
    output_args = conv_result(1:len);
end

