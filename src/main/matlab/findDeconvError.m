function [ out_signal, out_error ] = findDeconvError( blur, psf, signal, over_estimate_bias, default_signal, default_error )
    reconv = conv_cut(psf,signal);
    cblur = cumsum(blur);
    creconv = cumsum(reconv);
    len = max(size(blur));
    d = cblur-creconv;
    for i=1:len, 
        if d(i) > 0
            d(i) = over_estimate_bias*d(i)*d(i);
        else
            d(i) = d(i)*d(i);
        end
    end
    error = sum(d);
    if error < default_error
        out_signal = signal;
        out_error = error;
    else
        out_signal = default_signal;
        out_error = default_error;
    end
end

