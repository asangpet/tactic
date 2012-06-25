function [ output_args ] = convolve( a, b )
    output_args = conv(a,b,'same');
end

