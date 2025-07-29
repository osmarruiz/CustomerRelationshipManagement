import * as React from 'react';
import dayjs from 'dayjs';

export interface IDurationFormat {
  value: any;
  blankOnInvalid?: boolean;
}

export const DurationFormat = ({ value, blankOnInvalid }: IDurationFormat) => {
  if (blankOnInvalid && !value) {
    return null;
  }

  return (
    <span title={value}>
      {dayjs
        .duration(value)
        .humanize()}
    </span>
  );
};
